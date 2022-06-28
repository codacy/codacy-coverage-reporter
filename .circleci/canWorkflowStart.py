#!/usr/bin/python3

# This script checks if any circleci job is currently running in a target branch.
# It returns on the first positive match it encounters, in order to reduce executing time.
#
# It covers these scenarios:
# - A pipeline has a running job for a specific branch.
# - If any worflow with the same name as the target one has any running job, it will be considered as a match, to prevent from excluding initial and setup jobs.
#
# It does NOT cover the following corner-case scenarios and it will return false negatives:
# - If any worflow with the same branch as the target one has started but its jobs haven't started yet.
# - This script will not ensure a FIFO queue. If a new job starts when the checks of the older ones are "sleeping", it can start before all of them and make them eventually fail with a timeout.

import argparse
import http.client
import json
import signal
import time
import traceback

class timeout:
    def __init__(self, seconds):
        self.seconds = seconds
    def handle_timeout(self, signum, frame):
        raise TimeoutError
    def __enter__(self):
        signal.signal(signal.SIGALRM, self.handle_timeout)
        signal.alarm(self.seconds)
    def __exit__(self, type, value, traceback):
        signal.alarm(0)

def canWorkflowStart(slug: str, targetBranchName: str, currentWorkflowId: str) -> bool:
    
    targetWorkflowName = getWorkflowName(currentWorkflowId)
    
    for pipelineId in getFilteredPipelineIds(slug, targetBranchName):

        worflows = getWorkflows(pipelineId)
        workflowsWithTargetBranch = []

        for workflow in worflows:
            if workflow["id"] != currentWorkflowId:
                workflowsWithTargetBranch.append(workflow)

        for workflow in workflowsWithTargetBranch:
            for job in getJobs(workflow["id"]):
                if(job["status"] == "running"):
                    print(f'Another workflow with the same name [{workflow["name"]}] has a job with name [{job["name"]}] still running, so another workflow should not start yet.')
                    return False
    
    print("All clear, job can start.")
    return True

def debugPrint(message: str):
    if(isDebug):
        print(message)

def getCommandLineArguments():

    defaultSlug = "gh/codacy/codacy-coverage-reporter"
    defaultSleepValue = 2
    defaultTimeoutValue = 40

    parser = argparse.ArgumentParser(
        description='This script checks if any circleci job is currently running in a target branch.',
        add_help=True)
    parser.add_argument('-d', required=False, default=False, 
        action='store_true', dest='debugMode',
        help='Debug mode. Prints responses and other debug information.')
    parser.add_argument('-b', required=True, 
        action='store', dest='targetBranchName',
        help='Target branch.')
    parser.add_argument('-k', required=True,
        action='store', dest='apiToken',
        help='API token key.')
    parser.add_argument('-s', required=False, default=defaultSlug,
        action='store', dest='slug',
        help=f'Project slug (default: {defaultSlug}).')
    parser.add_argument('-l','--sleep', required=False, default=defaultSleepValue,
        action='store', dest='sleep', type=int,
        help=f'Sleep time in minutes (default: {defaultSleepValue}).')
    parser.add_argument('-t', required=False, default=defaultTimeoutValue,
        action='store', dest='timeout', type=int,
        help=f'Timeout in minutes (default: {defaultTimeoutValue}).')
    parser.add_argument('-w', required=True,
        action='store', dest='currentWorkflowId',
        help='Current worflow id.')
    
    return parser.parse_args()

def getIds(jsonResponseData):
    ids = []
    for item in jsonResponseData:
        ids.append(item["id"])
    return ids

def getJobs(workflowId: str):
    return getResponseItems(f"workflow/{workflowId}/job")

def getFilteredPipelineIds(slug: str, targetBranchName: str):
    ids = []
    for item in getResponseItems(f"project/{slug}/pipeline"):
        if "branch" in item["vcs"] and item["vcs"]["branch"] == targetBranchName:
            ids.append(item["id"])
    return ids

def getResponse(url: str) -> str:
    headers = { "Circle-Token": f"{apiToken}" }
    conn = http.client.HTTPSConnection("circleci.com")
    conn.request("GET", f"/api/v2/{url}", headers=headers)
    response = conn.getresponse().read().decode("utf-8")
    debugPrint(f"\nResponse output: \n{response}\n")
    return json.loads(response)

def getResponseItems(url: str) -> str:
    return getResponse(url)["items"]

def getWorkflowName(workflowId: str) -> str:
    return getResponse(f"workflow/{workflowId}")["name"]

def getWorkflows(pipelineId: str):
    return getResponseItems(f"pipeline/{pipelineId}/workflow")

def getWorkflowIds(pipelineId: str):
    return getIds(getResponseItems(f"pipeline/{pipelineId}/workflow"))

def minutesToSeconds(minutes: int) -> int:
    return minutes * 60

try:
    args = getCommandLineArguments()
    global apiToken
    apiToken = args.apiToken
    global isDebug
    isDebug = args.debugMode
    sleepValue = minutesToSeconds(args.sleep)
    timeoutValue = minutesToSeconds(args.timeout)

    debugPrint("\nArguments being used:")
    debugPrint(f"slug: [{args.slug}].")
    debugPrint(f"target branch: [{args.targetBranchName}].")
    debugPrint(f"current workflow ID: [{args.currentWorkflowId}].")
    debugPrint(f"timeout value in seconds: [{timeoutValue}] and sleep value in seconds: [{sleepValue}].")

    with timeout(timeoutValue):
        while not canWorkflowStart(args.slug, args.targetBranchName, args.currentWorkflowId):
            print(f"Going to sleep for [{args.sleep}] minutes.")
            time.sleep(sleepValue)

except TimeoutError:
    raise SystemExit(f"The script timed out after more than [{args.timeout}] minutes.")
except Exception:
    traceback.print_exc()
    print("\nThe script exited with an error, but the workflow will now continue anyway.")