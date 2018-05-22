package promeureka.model

data class PrometheusItem(val labels: PrometheusLabel, val targets: List<String>)