package net.swantrip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import net.swantrip.chouti.ChoutiCrawler;

@SpringBootApplication
public class SwantripApplication implements CommandLineRunner {

	private static Logger log = LoggerFactory.getLogger(SwantripApplication.class);

	public static void main(String[] args) throws Exception {
		SpringApplication.run(SwantripApplication.class, args);
	}

	@Autowired
	private RestTemplateBuilder builder;

	@Bean
	public RestTemplate restTemplate() {
		return builder.build();
	}

	@Autowired
	private TuniuCrawler tuniuCrawler;
	
	@Autowired
	private ChoutiCrawler choutiCrawler;

	@Override
	public void run(String... args) throws Exception {
		log.warn("application run");
		choutiCrawler.crawl_all();
//		if (args == null || args.length == 0) {
//			log.error("no args");
//		} else {
//			if ("crawl".equals(args[0])) {
//				tuniuCrawler.crawl_all();
//			}
//		}
	}
}
