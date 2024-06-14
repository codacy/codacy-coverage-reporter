package com.codacy.model.configuration

import org.scalatest.EitherValues
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class CommitUUIDSpec extends AnyWordSpec with Matchers with EitherValues {
  "isValid" should {
    "approve commit uuids with 40 hexadecimal chars" in {
      CommitUUID.fromString("ce280928adbdc852b8eefb0c57cf400f5f95db01") shouldBe Symbol("right")
      CommitUUID.fromString("ce280928adbdc852b8eefb0c57cf400f5f95db0") shouldBe Symbol("left")
      CommitUUID.fromString("ce280928adbdc852b8eefb0c57cf400f5f95db012") shouldBe Symbol("left")
      CommitUUID.fromString("40 random characters that are not hexa!!") shouldBe Symbol("left")
    }
  }
}
