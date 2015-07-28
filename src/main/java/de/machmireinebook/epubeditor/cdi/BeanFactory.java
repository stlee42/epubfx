package de.machmireinebook.epubeditor.cdi;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

/**
 * User: mjungierek
 * Date: 22.03.14
 * Time: 14:51
 */
public class BeanFactory
{
    private BeanManager beanManager;
    private static BeanFactory instance;

    private BeanFactory(BeanManager beanManager)
    {
        this.beanManager = beanManager;
        instance = this;
    }

    public static void initialize(BeanManager beanManager)
    {
        new BeanFactory(beanManager);
    }

    public static BeanFactory getInstance()
    {
        if (instance == null)
        {
            throw new NotInitializedException("BeanFactory not initialized");
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type)
    {
        Bean<T> bean = (Bean<T>) beanManager.resolve(beanManager.getBeans(type));
        CreationalContext<T> creationalContext = beanManager.createCreationalContext(bean);
        return (T) beanManager.getReference(bean, type, creationalContext);
    }

    private static class NotInitializedException extends RuntimeException
    {
        public NotInitializedException(String message)
        {
            super(message);
        }
    }
}
