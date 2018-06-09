package br.com.project.bean.geral;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.web.context.request.FacesRequestAttributes;

@SuppressWarnings("unchecked")
public class ViewScope implements Scope, Serializable {

	private static final long serialVersionUID = 1L;

	public static String VIEW_SCOPE_CALLBACKS = "viewScope.callBacks";

	// vai retornar a instancia da tela para a sessao (scopo)
	@Override
	public Object get(String name, ObjectFactory<?> objectFactory) {
		Object instance = getViewMap().get(name);
		if (instance == null) {
			instance = objectFactory.getObject();
			getViewMap().put(name, instance);
		}

		return instance;
	}

	// Carrega o ID que identifica o scopo
	@Override
	public String getConversationId() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		FacesRequestAttributes facesRequestAttributes = new FacesRequestAttributes(facesContext);

		return facesRequestAttributes.getSessionId() + "-" + facesContext.getViewRoot().getViewId();
	}

	// Registra a destruicao do scopo
	@Override
	public void registerDestructionCallback(String name, Runnable runnable) {
		Map<String, Runnable> callbacks = (Map<String, Runnable>) getViewMap().get(VIEW_SCOPE_CALLBACKS);

		if (callbacks != null) {
			callbacks.put(VIEW_SCOPE_CALLBACKS, runnable);
		}

	}

	// Remove o objeto do scopo
	@Override
	public Object remove(String name) {
		Object instance = getViewMap().remove(name);
		if (instance != null) {
			Map<String, Runnable> callBacks = (Map<String, Runnable>) getViewMap().get(VIEW_SCOPE_CALLBACKS);
			if (callBacks != null) {
				callBacks.remove(name);
			}
		}

		return instance;
	}

	// Resolve a referencia onde ira retornar o objeto
	@Override
	public Object resolveContextualObject(String name) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		FacesRequestAttributes facesRequestAttributes = new FacesRequestAttributes(facesContext);

		return facesRequestAttributes.resolveReference(name);
	}

	// getViewRoot()
	// Retorna o componente raiz que esta associado a esta solicitacao(request).
	// getViewMap() Retorna um Map que atua como a interface para o armazenamento de
	// dados.
	private Map<String, Object> getViewMap() {
		return FacesContext.getCurrentInstance() != null ? FacesContext.getCurrentInstance().getViewRoot().getViewMap()
				: new HashMap<String, Object>();
	}

}
