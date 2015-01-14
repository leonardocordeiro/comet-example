package br.com.leonardo.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns={"/comet"}, asyncSupported=true)
public class CometServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	private Queue<AsyncContext> clientes = new ConcurrentLinkedQueue<>();
	private BlockingQueue<String> mensagens = new LinkedBlockingQueue<>();
	
	@Override
	public void init() throws ServletException {
		Executors.newSingleThreadExecutor().submit(new Runnable() {
			public void run() {
				while(true) {
					try {
						//bloqueia a thread até entrar alguma mensagem na fila
						String mensagem = mensagens.take();
						enviar(mensagem);
					} catch(InterruptedException e) { 
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	private void enviar(String mensagem) {
		for(AsyncContext cliente : clientes) { 
			enviar(cliente, mensagem);
		}
	}
	
	private void enviar(AsyncContext cliente, String mensagem) {
		HttpServletResponse response = (HttpServletResponse) cliente.getResponse();
		response.setHeader("content-type", "text/html; charset=UTF-8");
		try {
			PrintWriter writer = response.getWriter();
			writer.println(mensagem);
			writer.flush();
		
			// delegar uma thread para responder ao cliente
			// ao chamar complete, o cliente sairá da fila
			cliente.complete();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} 
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		AsyncContext cliente = req.startAsync();
		cliente.setTimeout(3000);
		cliente.addListener(new AsyncListener() {
			@Override
			public void onComplete(AsyncEvent event) throws IOException {
				clientes.remove(event.getAsyncContext());
			}

			@Override
			public void onError(AsyncEvent event) throws IOException {
				event.getAsyncContext().complete();
				clientes.remove(event.getAsyncContext());
			}

			@Override
			public void onStartAsync(AsyncEvent event) throws IOException {
			}

			@Override
			public void onTimeout(AsyncEvent event) throws IOException {
				event.getAsyncContext().complete();
				clientes.remove(event.getAsyncContext());
			}
		});
		
		clientes.add(cliente);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String mensagem = req.getParameter("mensagem");
		mensagens.add(mensagem);
	}
}
