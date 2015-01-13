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
	
	private BlockingQueue<String> mensagens = new LinkedBlockingQueue<>();
	private ConcurrentLinkedQueue<AsyncContext> clientes = new ConcurrentLinkedQueue<>();
	
	@Override
	public void init() throws ServletException {
		Executors.newSingleThreadExecutor().execute(new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					try {
						String mensagem = mensagens.take();
						enviar(mensagem);
					} catch(InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}

		});
	}

	private void enviar(String mensagem) {
		for (AsyncContext ctx : clientes) {
			try {
				HttpServletResponse res = (HttpServletResponse) ctx.getResponse();
				res.setContentType("text/html");
				
				PrintWriter writer = res.getWriter();
				writer.println(mensagem + "<br>");
				writer.flush();
				
				writer.close();
				ctx.complete();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		AsyncContext ctx = req.startAsync();
		ctx.setTimeout(30000);
		ctx.addListener(new AsyncListener() {
			
			@Override
			public void onTimeout(AsyncEvent arg0) throws IOException {
				clientes.remove(arg0.getAsyncContext());
			}
			
			@Override
			public void onStartAsync(AsyncEvent arg0) throws IOException {
			}
			
			@Override
			public void onError(AsyncEvent arg0) throws IOException {
				clientes.remove(arg0.getAsyncContext());
			}
			
			@Override
			public void onComplete(AsyncEvent arg0) throws IOException {
				clientes.remove(arg0.getAsyncContext());				
			}
		});
		clientes.add(ctx);
		
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String mensagem = req.getParameter("mensagem");
		mensagens.add(mensagem);
	}
}
