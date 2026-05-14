CREATE TABLE IF NOT EXISTS equipement_photo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_equipement INT NOT NULL,
    chemin_photo VARCHAR(255) NOT NULL,
    CONSTRAINT fk_equipement_photo FOREIGN KEY (id_equipement) REFERENCES equipement(id) ON DELETE CASCADE
);
