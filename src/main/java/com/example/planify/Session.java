package com.example.planify;

/**
 * Класс Session реализует паттерн Singleton для хранения информации
 * о текущем авторизованном пользователе в приложении.
 */
public class Session {
    // Единственный экземпляр класса
    private static Session instance;

    // Поле для хранения информации о текущем пользователе
    private User loggedInUser;

    //Последний открытый проект
    private String lastOpenedProject;
    /**
     * Приватный конструктор предотвращает создание экземпляров класса извне.
     */
    private Session() {}

    /**
     * Метод для получения единственного экземпляра класса.
     * Реализует потокобезопасный доступ к объекту.
     *
     * @return экземпляр класса Session
     */
    public static synchronized Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    /**
     * Возвращает текущего авторизованного пользователя.
     *
     * @return объект User, представляющий текущего пользователя, или null, если пользователь не авторизован.
     */
    public User getLoggedInUser() {
        return loggedInUser;
    }

    /**
     * Устанавливает пользователя как текущего авторизованного.
     *
     * @param user объект User, представляющий авторизованного пользователя.
     */
    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
    }

    /**
     * Очищает информацию о текущем пользователе (выход из системы).
     */
    public void clearSession() {
        loggedInUser = null;
    }

    //последний открытый проект(страница задании)
    public void setLastOpenedProject(String projectName) {
        this.lastOpenedProject = projectName;
    }

    public String getLastOpenedProject() {
        return lastOpenedProject;
    }
}
