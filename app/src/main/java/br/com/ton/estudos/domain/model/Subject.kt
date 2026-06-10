package br.com.ton.estudos.domain.model

data class Subject(
    val id: Long = 0,
    val name: String,
    val color: String,
    val icon: String,
    val createdAt: Long = System.currentTimeMillis()
)
