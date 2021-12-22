package ru.ckateptb.tablecloth.temporary;

public enum TemporaryUpdateState {
    CONTINUE, // Продолжить обрабатывать временный объект
    REVERT // Вернуть временный объект в исходное положение
}
