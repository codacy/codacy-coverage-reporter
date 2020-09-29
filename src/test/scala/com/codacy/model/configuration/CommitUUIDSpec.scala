package com.codacy.model.configuration

import org.scalatest.{Matchers, WordSpec}

class CommitUUIDSpec extends WordSpec with Matchers {
  "isValid" should {
    "approve commit uuids with 40 hexadecimal chars" in {
      CommitUUID("ce280928adbdc852b8eefb0c57cf400f5f95db01").isValid shouldBe true
      CommitUUID("ce280928adbdc852b8eefb0c57cf400f5f95db0").isValid shouldBe false
      CommitUUID("ce280928adbdc852b8eefb0c57cf400f5f95db012").isValid shouldBe false
      CommitUUID("40 random characters that are not hexa!!").isValid shouldBe false
    }
  }
}
