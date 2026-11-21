-- V6: entradas de blog de demostracion (idempotente)
INSERT IGNORE INTO entradas_blog (
    autor_usuario_id,
    slug,
    titulo,
    resumen,
    imagen_url,
    bloques,
    publicada,
    publicada_en)
SELECT u.id,
       'rutina-skincare-diaria',
       'Rutina de skincare para todos los dias',
       'Tres pasos simples para mantener la piel hidratada y protegida durante la semana.',
       'Crema Hidratante Facial.jpg',
       CAST('[
          {"tipo": "parrafo", "texto": "Una rutina constante vale mas que muchos productos. Estos tres pasos cubren limpieza, hidratacion y proteccion sin recargar la piel."},
          {"tipo": "titulo", "texto": "Manana"},
          {"tipo": "lista", "items": [
             {"etiqueta": "Limpieza", "texto": "Usa un limpiador suave con agua tibia para retirar la oleosidad de la noche."},
             {"etiqueta": "Hidratacion", "texto": "Aplica la crema hidratante con acido hialuronico sobre la piel ligeramente humeda."},
             {"etiqueta": "Proteccion", "texto": "Termina con protector solar y renueva su aplicacion durante el dia."}
          ]},
          {"tipo": "titulo", "texto": "Noche"},
          {"tipo": "parrafo", "texto": "Repite la limpieza y vuelve a hidratar. Si usaste maquillaje, retiralo antes de dormir para evitar obstruir los poros."}
       ]' AS JSON),
       TRUE,
       NOW()
FROM usuario u
WHERE u.correo = 'admin@kateye.cl';

INSERT IGNORE INTO entradas_blog (
    autor_usuario_id,
    slug,
    titulo,
    resumen,
    imagen_url,
    bloques,
    publicada,
    publicada_en)
SELECT u.id,
       'como-elegir-tu-base-liquida',
       'Como elegir tu base liquida',
       'Cobertura, acabado y tono: una guia breve para acertar con tu base.',
       'Base líquida mate.jpg',
       CAST('[
          {"tipo": "parrafo", "texto": "La base correcta depende de lo que buscas: cobertura natural para el dia a dia o un acabado mas uniforme para ocasiones especiales."},
          {"tipo": "titulo", "texto": "Tres puntos clave"},
          {"tipo": "lista", "items": [
             {"etiqueta": "Cobertura", "texto": "Las formulas modulables permiten construir capas sin perder naturalidad."},
             {"etiqueta": "Acabado", "texto": "Mate para piel mixta o grasa; luminoso para piel seca o cansada."},
             {"etiqueta": "Tono", "texto": "Prueba sobre la mandibula con luz natural y elige el tono mas cercano a tu cuello."}
          ]},
          {"tipo": "parrafo", "texto": "Aplica con esponja humeda para un resultado parejo y sella con polvo solo en las zonas donde lo necesites."}
       ]' AS JSON),
       TRUE,
       NOW()
FROM usuario u
WHERE u.correo = 'admin@kateye.cl';
