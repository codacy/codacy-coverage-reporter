package com.codacy.transformation

import com.codacy.api.CoverageReport

trait Transformation {

  def execute(report: CoverageReport): CoverageReport

}
