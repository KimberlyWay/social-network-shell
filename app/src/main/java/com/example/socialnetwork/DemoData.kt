package com.example.socialnetwork

data class Person(val id: String, val name: String, val city: String, val avatar: Int = 0)
data class Post(val author: String, val time: String, val text: String, val picture: Int)
data class Profile(val name: String, val city: String, val about: String)
data class Message(val text: String, val outgoing: Boolean, val timestamp: Long)

object DemoData {
    const val SAVED_CHAT = "saved"

    val people = listOf(
        Person("anna", "Анна Смирнова", "Самара", R.drawable.avatar_anna),
        Person("maxim", "Максим Орлов", "Казань", R.drawable.avatar_maxim),
        Person("olga", "Ольга Иванова", "Самара"),
        Person("denis", "Денис Волков", "Москва"),
        Person("maria", "Мария Соколова", "Казань"),
        Person("ivan", "Иван Петров", "Санкт-Петербург")
    )

    // Учебная лента. Тексты и картинки встроены в APK и не загружаются из сети.
    val posts = listOf(
        Post("Анна Смирнова", "Сегодня, 10:15",
            "После занятий выбрались на прогулку. Иногда лучший план на вечер — просто пройтись у воды.",
            R.drawable.post_river),
        Post("Максим Орлов", "Сегодня, 09:40",
            "Собрал первое приложение в Android Studio! Пока всё просто, но поиск и кнопки уже работают.",
            R.drawable.post_code),
        Post("Ольга Иванова", "Вчера, 18:20",
            "Осень во дворе университета. Скоро зачёты, а пока есть время посидеть с друзьями в парке.",
            R.drawable.post_campus)
    )

    fun person(id: String): Person? = people.find { it.id == id }

    fun initials(name: String): String = name.trim().split(Regex("\\s+"))
        .filter { it.isNotEmpty() }.take(2).joinToString("") { it.take(1) }.uppercase()
}
