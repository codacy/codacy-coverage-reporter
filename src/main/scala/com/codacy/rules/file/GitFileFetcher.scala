package com.codacy.rules.file

import com.codacy.api.helpers.vcs.GitClient

import java.io.File
import scala.util.{Failure, Success}

object GitFileFetcher {

  def forCommit(commitSha: String): Either[String, Seq[String]] = {

    new GitClient(new File(System.getProperty("user.dir"))).getRepositoryFileNames(commitSha) match {
      case Failure(e) =>
        Left(s"Could not retrieve files from local Git directory, error message: ${e.getMessage}")
      case Success(files) => Right(files)
    }
  }

}
