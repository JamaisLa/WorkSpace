package com.workspace;

import com.workspace.model.TypeEquipement;
import com.workspace.repository.TypeEquipementRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class WorkspaceLiteApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkspaceLiteApplication.class, args);
	}

	@Bean
	ApplicationRunner initialiserTypesEquipement(TypeEquipementRepository repository) {
		return args -> List.of(
				"Souris", "Clavier", "Écran", "Ordinateur", "Tablette", "Téléphone",
				"Vidéoprojecteur", "Webcam", "Microphone", "Enceinte",
				"Système de visioconférence", "Imprimante", "Tableau", "Mobilier",
				"Câble / adaptateur", "Autre"
		).forEach(nom -> {
			if (!repository.existsByNomIgnoreCase(nom)) {
				repository.save(new TypeEquipement(nom));
			}
		});
	}

}
