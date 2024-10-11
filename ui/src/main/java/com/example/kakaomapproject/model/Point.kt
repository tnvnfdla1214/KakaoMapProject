package com.example.kakaomapproject.model

data class Point(
    val lng: Double,
    val lat: Double
) {
    companion object {
        fun parsePoints(points: String): List<Point> {
            return points
                .trim() // 앞뒤 공백 제거
                .split(" ") // 공백을 기준으로 각 Point 분리
                .map { point ->
                    val coordinates = point.split(",")
                    Point(
                        lng = coordinates[0].toDouble(),  // 첫 번째 값은 경도
                        lat = coordinates[1].toDouble()   // 두 번째 값은 위도
                    )
                }
        }
    }
}