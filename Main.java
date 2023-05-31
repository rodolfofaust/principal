package oystr.java.test;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/*
 * The application MUST be developed in Java using the concepts of Object Oriented Programming. 
 * You may also use concepts such as Dependency Injection and Inversion of Control.
 * You may modify the main method to better suit your approach, but you MAY NOT change the concept itself.
 */
public class Main {
	static final Logger logger = Logger.getLogger(Main.class.getName());
	private Document doc;

	public Main(Document doc) {
		this.doc = doc;

	}


	public static void main(String[] args) {
		/*
		 * TODO: initialize a bot impl class and decide how it's going to be applied/used here.
		 */
		System.out.println( "[main] ------INICIO------");
		System.out.println( "------------------------------------------------------------");
		Bot b = null;
		int a = 0;

		/*
		 * TODO: If any of these pages does not work, you may look to new ones in the root page for each domain.
		 */
		String [] urls = new String[] {
				//"https://www.agrofy.com.br/trator-mahindra-5050-novo-49cv-4x4-4cilindros.html",
				//"https://www.agrofy.com.br/trator-mahindra-6075-novo-80cv-4x4-4cilindros.html",
				"https://www.tratoresecolheitadeiras.com.br/veiculo/uberlandia/mg/plataforma-colheitadeira/gts/flexer-xs-45/2023/45-pes/draper/triamaq-tratores/1028839",
				//"https://www.tratoresecolheitadeiras.com.br/veiculo/uberlandia/mg/plataforma-colheitadeira/gts/produttiva-1250/2022/caracol/12-linhas/triamaq-tratores/994257",
				//"https://www.mercadomaquinas.com.br/anuncio/218193-escavadeira-caterpillar-320c-2006-aruja-sp",
				//"https://www.mercadomaquinas.com.br/anuncio/214554-pa-carregadeira-caterpillar-950h-2012-curitiba-pr"
		};

		for(String s : urls) {
			System.out.println("LINK: "+ s);
			a++;
			try {
				Document doc = Jsoup.connect(s).get();
				Main parsePage = new Main(doc);
				parsePage.getPage(a);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//Machine m = b.fetch(s);

			/*
			 * TODO: print/output data from mA, mB and mC here
			 */
			System.out.println( "------------------------------------------------------------");
		}
		System.out.println( "[main] ------FIM------");
	}


	private void getPage(int a) {
		//if (a < 3) { //agrofy
		//	Elements conteudoPagina = doc.getElementsByClass("page-content");
		//	getContentAgrofy(conteudoPagina);
		//}

	//	if (a > 2 && a < 5) { //agrofy
			Elements conteudoPagina = doc.getElementsByTag("div");
			getContentTrator(conteudoPagina);
		//}

	}

	@SuppressWarnings("unused")
	private void getContentTrator(Elements conteudosPagina) {
		String title = null, model = null, contract = null, year = null, 
				workHours = null, city = null, price = null, photo = null,
				name = null;
		/*
	1. Model
	2. Contract type (rent or sale)
	3. Make
	4. Year
	5. Worked hours
	6. City
	7. Price
	8. Photo/Picture
		 */
		for (Element conteudoPagina : conteudosPagina) {
			if (conteudoPagina.attr("id").equals("page-content")) {
				model = conteudoPagina.getElementsByTag("h1").text(); //ta certo
				price = conteudoPagina.getElementsByClass("product-single__price").text(); //ta certo
				System.out.println("MODELO: "+ model);
				System.out.println("PREÇO: "+ price);
			}
			
			Elements produtosCarousels = conteudoPagina.getElementsByTag("div");
			for (Element produtoCarousel : produtosCarousels) {
				Elements imgs = produtoCarousel.getElementsByTag("img");
				if (photo == null) { 
					photo = getImage(imgs);
				}	
			}
			
			Elements produtosInfo = conteudoPagina.getElementsByTag("section");
			for (Element produtoInfo : produtosInfo) {
				if (produtoInfo.attr("id").equals("ProductInfo")) {
					name = conteudoPagina.getElementsByTag("h1").text(); //ta certo
					System.out.println("NOME: "+ name);
				}
				
				Elements lis = produtoInfo.getElementsByTag("li");
				for (Element li : lis) {
					String div = li.getElementsByTag("div").text();
					String ul = li.getElementsByTag("ul").text();	

					if (div.equals("Modelo")) {
						title = div; //ta certo
						model = ul; //ta certo
						System.out.println( title.toUpperCase()+": "+model);
					} else if (div.equals("Ano de fabricação")) {
						title = div; //ta certo
						year = ul; //ta certo
						System.out.println( title.toUpperCase()+": "+year);
					}
				}
			}
		}
		System.out.println("IMAGEM: "+photo);
	}

	private String getImage(Elements imgs) {
		String imagem = null;
		for (Element img : imgs) {
			if (img.attr("loading").equals("auto")) {
				imagem = img.attr("src");
			}
		}
		return imagem;
	}
	
	

	@SuppressWarnings("unused")
	private void getContentAgrofy(Elements conteudosPagina) {
		String title = null, model = null, contract = null, year = null, 
				workHours = null, city = null, price = null, photo = null,
				name = null;
		/*
	1. Model
	2. Contract type (rent or sale)
	3. Make
	4. Year
	5. Worked hours
	6. City
	7. Price
	8. Photo/Picture
		 */
		for (Element conteudoPagina : conteudosPagina) {
			
			if (conteudoPagina.attr("id").equals("page-content")) {
				String html = conteudoPagina.html();
				System.out.println("html: "+ html);
			}
			
			Elements produtosCarousels = conteudoPagina.getElementsByTag("div");
			for (Element produtoCarousel : produtosCarousels) {
				Elements imgs = produtoCarousel.getElementsByTag("img");
				if (photo == null) { 
					photo = getImage(imgs);
				}	
			}
			
			Elements produtosInfo = conteudoPagina.getElementsByTag("section");
			for (Element produtoInfo : produtosInfo) {
				if (produtoInfo.attr("id").equals("ProductInfo")) {
					name = conteudoPagina.getElementsByTag("h1").text(); //ta certo
					System.out.println("NOME: "+ name);
				}
				
				Elements lis = produtoInfo.getElementsByTag("li");
				for (Element li : lis) {
					String div = li.getElementsByTag("div").text();
					String ul = li.getElementsByTag("ul").text();	

					if (div.equals("Modelo")) {
						title = div; //ta certo
						model = ul; //ta certo
						System.out.println( title.toUpperCase()+": "+model);
					} else if (div.equals("Ano de fabricação")) {
						title = div; //ta certo
						year = ul; //ta certo
						System.out.println( title.toUpperCase()+": "+year);
					}
				}
			}
		}
		System.out.println("IMAGEM: "+photo);
	}
}








