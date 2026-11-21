-- V4: catalogo base (categorias, productos, imagenes y ofertas de ejemplo)

INSERT IGNORE INTO categoria (slug, nombre, orden) VALUES
    ('skincare', 'Skincare', 1),
    ('maquillaje', 'Maquillaje', 2),
    ('perfumes', 'Perfumes', 3),
    ('cabello', 'Cabello', 4),
    ('accesorios', 'Accesorios', 5);

INSERT IGNORE INTO producto (codigo, nombre, descripcion, precio, stock, stock_critico, categoria_id, activo)
SELECT v.codigo,
       v.nombre,
       v.descripcion,
       v.precio,
       v.stock,
       v.stock_critico,
       c.id,
       TRUE
FROM (
    SELECT 'KAT-001' AS codigo, 'Sérum Vitamina C' AS nombre, 'Sérum facial iluminador con 15% de vitamina C estabilizada.' AS descripcion, 19990 AS precio, 25 AS stock, 5 AS stock_critico, 'skincare' AS slug
    UNION ALL SELECT 'KAT-002', 'Crema Hidratante Ácido Hialurónico', 'Hidratación profunda de 24 horas para todo tipo de piel.', 14990, 40, 8, 'skincare'
    UNION ALL SELECT 'KAT-003', 'Protector Solar SPF50', 'Protección solar de amplio espectro, textura ligera no grasa.', 12990, 30, 6, 'skincare'
    UNION ALL SELECT 'KAT-004', 'Base Líquida HD', 'Cobertura media modulable con acabado natural.', 16990, 20, 4, 'maquillaje'
    UNION ALL SELECT 'KAT-005', 'Labial Mate Terracota', 'Labial de larga duración con acabado mate aterciopelado.', 16990, 45, 10, 'maquillaje'
    UNION ALL SELECT 'KAT-006', 'Paleta Sombras Nude', 'Doce tonos neutros de alta pigmentación.', 22990, 18, 4, 'maquillaje'
    UNION ALL SELECT 'KAT-007', 'Eau de Parfum Floral', 'Fragancia floral con notas de peonía y jazmín.', 39990, 12, 3, 'perfumes'
    UNION ALL SELECT 'KAT-008', 'Perfume Amaderado Intenso', 'Fragancia amaderada con notas de sándalo y ámbar.', 44990, 10, 3, 'perfumes'
    UNION ALL SELECT 'KAT-009', 'Eau de Toilette Cítrica', 'Fragancia fresca con notas de bergamota y limón.', 39990, 15, 3, 'perfumes'
    UNION ALL SELECT 'KAT-010', 'Shampoo Reparación Intensa', 'Shampoo sin sulfatos para cabello dañado.', 8990, 50, 10, 'cabello'
    UNION ALL SELECT 'KAT-011', 'Mascarilla Capilar Keratina', 'Tratamiento intensivo de keratina para cabello seco.', 11990, 35, 8, 'cabello'
    UNION ALL SELECT 'KAT-012', 'Aceite Capilar Argán', 'Aceite nutritivo de argán para puntas abiertas.', 9990, 28, 6, 'cabello'
    UNION ALL SELECT 'KAT-013', 'Esponja de Maquillaje', 'Esponja suave para aplicar base y corrector.', 4990, 60, 12, 'accesorios'
    UNION ALL SELECT 'KAT-014', 'Brocha Difuminadora', 'Brocha de cerdas sintéticas para sombras y rubor.', 7990, 40, 8, 'accesorios'
    UNION ALL SELECT 'KAT-015', 'Neceser Beauty', 'Neceser compacto y resistente al agua.', 9990, 22, 5, 'accesorios'
) v
JOIN categoria c ON c.slug = v.slug;

INSERT INTO producto_imagen (producto_id, url, orden)
SELECT p.id, CONCAT('/img/productos/', LOWER(p.codigo), '.jpg'), 0
FROM producto p
WHERE p.codigo LIKE 'KAT-%'
  AND NOT EXISTS (
      SELECT 1 FROM producto_imagen pi WHERE pi.producto_id = p.id AND pi.orden = 0
  );

INSERT INTO oferta (producto_id, precio_anterior, precio_oferta, desde, hasta)
SELECT p.id,
       v.precio_anterior,
       v.precio_oferta,
       NOW() - INTERVAL 1 DAY,
       NOW() + INTERVAL 30 DAY
FROM (
    SELECT 'KAT-001' AS codigo, 19990 AS precio_anterior, 15990 AS precio_oferta
    UNION ALL SELECT 'KAT-005', 16990, 13990
    UNION ALL SELECT 'KAT-009', 39990, 34990
) v
JOIN producto p ON p.codigo = v.codigo
WHERE NOT EXISTS (
    SELECT 1
    FROM oferta o
    WHERE o.producto_id = p.id
      AND o.precio_oferta = v.precio_oferta
);
