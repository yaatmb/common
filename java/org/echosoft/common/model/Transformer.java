package org.echosoft.common.model;

/**
 * Преобразовывает объекты исходного класса в объекты целевого класса.
 *
 * @author Anton Sharapov
 */
public interface Transformer<S,D> {

    /**
     * Выполняет преобразование указанного объекта.
     * @param value  исходный объект.
     * @return  трансформированный объект.
     */
    public D transform(S value);
}
