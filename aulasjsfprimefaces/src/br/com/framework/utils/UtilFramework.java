package br.com.framework.utils;

import java.io.Serializable;

import org.springframework.stereotype.Component;

/**
 * Responsavel por carregar qual usuario que esta fazendo a alteracao
 * 
 * @author Rodrigo
 */

@Component
public class UtilFramework implements Serializable {

	private static final long serialVersionUID = 1L;

	private static ThreadLocal<Long> threadLocal = new ThreadLocal<Long>();

	public synchronized static ThreadLocal<Long> getThreadLocal() {
		return threadLocal;
	}

}
