package com.example.test

object Constants {
    const val MEASUREMENT_PATTERN = """(?<key_value>\b\w+(?:\s+\w+)*\b)(?:\s+(?:is|equals|measures|of|at|reads|shows|about|approximately|comes\s+to|was))?\s+(?<number_value>\d+(?:\.\d+)?)\s*(?:cm|centimeters|centimeter|c\.m\.|cms)?"""
}