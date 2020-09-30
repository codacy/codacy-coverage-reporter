package com.codacy.model.configuration

import org.scalatest.{EitherValues, Matchers, WordSpec}

class CommitUUIDSpec extends WordSpec with Matchers with EitherValues {
  "isValid" should {
    "approve commit uuids with 40 hexadecimal chars" in {
      CommitUUID.fromString("ce280928adbdc852b8eefb0c57cf400f5f95db01") shouldBe 'right
      CommitUUID.fromString("ce280928adbdc852b8eefb0c57cf400f5f95db0") shouldBe 'left
      CommitUUID.fromString("ce280928adbdc852b8eefb0c57cf400f5f95db012") shouldBe 'left
      CommitUUID.fromString("40 random characters that are not hexa!!") shouldBe 'left
    }
  }
}
