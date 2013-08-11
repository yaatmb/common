package org.echosoft.common.data;

/**
 * <p>Один из способов поточной обработки данных, когда обработчик передается в виде callback поставщику данных.</p>
 * <p>По сравнению с {@link Consumer} данный интерфейс дает предоставляет обработчику оценить возможность
 * выполнения требуемой работы и, при необходимости, отказаться от нее (см. метод {@link #init(D)}).</p>
 * TODO: при миграции библиотеки на Java 7 данный интерфейс должен быть расширен от java.lang.AutoCloseable
 *
 * @author Anton Sharapov
 */
public interface ConditionalConsumer<D, T> {

    /**
     * Вызывается перед началом обработки массива данных.
     * Является подходящим местом для инициализации всех требуемых обработчиком ресурсов.
     *
     * @param descriptor  содержит некоторую сводную информацию о предстоящей обработке данных.
     * @return <code>true</code> - если обработчик подтверждает готовность обрабатывать указанный поток данных;<br/>
     *         <code>false</code> - если обработчик отказывается обрабатывать указанный поток данных.
     *              В этом случае методы {@link #consume(T)} и {@link #close()} не вызываются.
     */
    public boolean init(D descriptor) throws Exception;

    /**
     * Основной метод. Вызывается для каждой записи обрабатываемого массива данных.
     *
     * @param record  текущая обрабатываемая запись.
     */
    public void consume(T record) throws Exception;

    /**
     * Вызывается по завершении обработки последнего элемента массива данных.
     * Является подходящим местом для освобождения всех ресурсов затребованных обработчиком.
     */
    public void close() throws Exception;
}
