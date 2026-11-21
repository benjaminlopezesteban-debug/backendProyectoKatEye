-- V5: columnas de fecha requeridas por el contenido
ALTER TABLE entradas_blog
    ADD COLUMN publicada_en DATETIME(6);

ALTER TABLE mensaje_contacto
    ADD COLUMN creado_en DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);
