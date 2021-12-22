package ru.ckateptb.tablecloth.temporary;

public interface Temporary {

    /**
     * Установить время, когда объект должен вернутся в исходное состояние
     * Установите -1 если время не ограничено
     * @param revertTime время в TimeMillis
     */
    void setRevertTime(long revertTime);

    /**
     * @return время в TimeMillis, когда объект должен вернутся в исходное состояние
     * -1 означает, что время не ограничено
     */
    long getRevertTime();

    /**
     * @param finalHandler - обработчик, который будет выполнен перед методом {@link #revert()}
     */
    void setFinalHandler(FinalHandler finalHandler);

    /**
     * @return обработчик, который будет выполнен перед методом {@link #revert()}
     */
    FinalHandler getFinalHandler();

    /**
     * Зарегистрировать объект в сервисе, для дальнейшей работы с ним
     */
    void register();

    /**
     * Начальная логика объекта, которая будет выполнена сразу после {@link #register()}
     */
    void init();

    /**
     * Данный метод вызывается каждый игровой тик
     * @return нужно ли возвращать объект в исходное состояние или необходимо продолжить обработку
     */
    TemporaryUpdateState update();

    /**
     * Отменяет все изменения, которые внес данный объект (Возвращает его в исходное состояние)
     */
    void revert();

    interface FinalHandler {
        void on();
    }
}

