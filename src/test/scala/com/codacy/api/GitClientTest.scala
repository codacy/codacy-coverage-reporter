package com.codacy.api

import com.codacy.api.helpers.vcs.GitClient
import java.nio.file.Paths
import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.OptionValues._

class GitClientTest extends AnyFlatSpec with Matchers {

  "GitClient" should "latestCommitUuid" in {

    val file = Paths.get("").toAbsolutePath.toFile

    val latest: Option[String] = new GitClient(file).latestCommitUuid()

    latest shouldNot be(None)

    latest.value shouldNot be(Symbol("empty"))
  }

}
