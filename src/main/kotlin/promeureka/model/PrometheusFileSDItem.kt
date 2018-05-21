package promeureka.model

data class PrometheusFileSDItem(val labels: PrometheusLabel, val targets: List<String>)