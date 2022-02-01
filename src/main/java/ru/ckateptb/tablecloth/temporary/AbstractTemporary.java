package ru.ckateptb.tablecloth.temporary;

import lombok.Getter;
import ru.ckateptb.tablecloth.ioc.IoC;

public abstract class AbstractTemporary implements Temporary {
    private long revertTime = -1;
    private FinalHandler finalHandler = null;
    @Getter
    private TemporaryService temporaryService;

    /**
     * Зарегистрировать объект в стандартном сервисе для временных объектов {@link TemporaryService}
     */
    public final void register() {
        this.temporaryService = IoC.get(TemporaryService.class);
        this.temporaryService.register(this);
    }

    @Override
    public long getRevertTime() {
        return this.revertTime;
    }

    @Override
    public void setRevertTime(long revertTime) {
        this.revertTime = revertTime;
    }

    @Override
    public FinalHandler getFinalHandler() {
        return this.finalHandler;
    }

    @Override
    public void setFinalHandler(FinalHandler finalHandler) {
        this.finalHandler = finalHandler;
    }

}
