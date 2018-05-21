package promeureka.model

data class PrometheusItem(val label: PrometheusLabel, val targets: List<String>)