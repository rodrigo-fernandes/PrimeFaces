package br.com.project.exception;

import java.util.Iterator;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationHandler;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;

import org.hibernate.SessionFactory;

import br.com.framework.hibernate.session.HibernateUtil;

/**
 * Classe generica para tratar os erros 
 * @author Rodrigo
 *
 */
public class CustomExceptionHandler extends ExceptionHandlerWrapper {

	private ExceptionHandler wrapperd;

	final FacesContext facesContext = FacesContext.getCurrentInstance();

	final Map<String, Object> requestMap = facesContext.getExternalContext().getRequestMap();

	final NavigationHandler navigationHandler = facesContext.getApplication().getNavigationHandler();

	public CustomExceptionHandler(ExceptionHandler exceptionHandler) {
		this.wrapperd = exceptionHandler;
	}

	// Sobreescreve o metodo ExceptionHandler que retorna a "pilha" de excecoes
	@Override
	public ExceptionHandler getWrapped() {
		return wrapperd;
	}

	// Sobreescreve o metodo handle que eh responsavel por manipular as excecoes do
	// JSF
	@Override
	public void handle() throws FacesException {
		final Iterator<ExceptionQueuedEvent> iterator = getUnhandledExceptionQueuedEvents().iterator();

		while (iterator.hasNext()) {
			ExceptionQueuedEvent event = iterator.next();
			ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();

			// Recupera a execao do contexto
			Throwable exeption = context.getException();

			// Aqui trabalhamos a execao
			try {
				requestMap.put("exceptionMessage", exeption.getMessage());

				if (exeption != null && exeption.getMessage() != null
						&& exeption.getMessage().indexOf("ConstraintViolationException") != -1) {

					FacesContext.getCurrentInstance().addMessage("msg", new FacesMessage(FacesMessage.SEVERITY_WARN,
							"Registro não pode ser removido por estar associado.", ""));
				} else if (exeption != null && exeption.getMessage() != null
						&& exeption.getMessage().indexOf("org.hibernate.StaleObjectStateException") != -1) {

					FacesContext.getCurrentInstance().addMessage("msg", new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"Registro foi atualizado ou excluído por outro usuário. " + "Consulte novamente", ""));
				} else {
					// Avisa o usuario do erro
					FacesContext.getCurrentInstance().addMessage("msg", new FacesMessage(FacesMessage.SEVERITY_FATAL,
							"O sistema se recuperou de um erro inesperado", ""));

					// Tranquiliza o usuario para que ele continue usando o sistema
					FacesContext.getCurrentInstance().addMessage("msg", new FacesMessage(FacesMessage.SEVERITY_INFO,
							"Você pode continuar usando o sistema normalmente!", ""));

					FacesContext.getCurrentInstance().addMessage("msg", new FacesMessage(FacesMessage.SEVERITY_FATAL,
							"O erro foi acusado por: \n" + exeption.getMessage(), ""));

					// PrimeFaces
					// Esse alert apenas é exibido se a pagina nao redirecionar
					org.primefaces.context.RequestContext.getCurrentInstance()
							.execute("alert('O sistema se recuperou de um erro inesperado.')");

					org.primefaces.context.RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(
							FacesMessage.SEVERITY_INFO, "Erro", "O sistema se recuperou de um erro inesperado..."));

					// Redireciona para a pagina de erro
					navigationHandler.handleNavigation(facesContext, null,
							"/error/error.jsf?faces-redirect=true&expired=true");

				}
				// Redenriza a pagina de erro e exibe as mensagens
				facesContext.renderResponse();

			} finally {
				SessionFactory sf = HibernateUtil.getSessionFactory();
				if (sf.getCurrentSession().getTransaction().isActive()) {
					sf.getCurrentSession().getTransaction().rollback();
				}

				// imprime o erro no console
				exeption.printStackTrace();

				iterator.remove();
			}
		}

		getWrapped().handle();
	}

}
