/**
 * Firebase Cloud Functions para PopcornTribu
 * Maneja el envío de notificaciones push cuando se crean
 * documentos en /notificaciones
 */

import {onDocumentCreated} from "firebase-functions/v2/firestore";
import {initializeApp} from "firebase-admin/app";
import {getMessaging} from "firebase-admin/messaging";
import * as logger from "firebase-functions/logger";

// Inicializar Firebase Admin
initializeApp();

/**
 * Función que se dispara cuando se crea un nuevo documento
 * en /notificaciones. Lee los datos del documento y envía
 * notificaciones push a los tokens especificados
 */
export const enviarNotificacion = onDocumentCreated(
  "notificaciones/{notifId}",
  async (event) => {
    const snap = event.data;

    if (!snap) {
      logger.warn("No hay datos en el snapshot");
      return;
    }

    const data = snap.data();

    if (!data) {
      logger.warn("No hay datos en la notificación");
      return;
    }

    const tokens: string[] = data.tokens || [];
    const titulo: string = data.titulo || "PopcornTribu";
    const mensaje: string = data.mensaje || "";
    const dataPayload = data.data || {};

    if (tokens.length === 0) {
      logger.info("No hay tokens para enviar la notificación");
      // Eliminar el documento ya que no hay destinatarios
      await snap.ref.delete();
      return;
    }

    logger.info(
      `Enviando notificación a ${tokens.length} dispositivos`
    );
    logger.info(`Título: ${titulo}, Mensaje: ${mensaje}`);

    // Construir el mensaje de notificación
    const message = {
      notification: {
        title: titulo,
        body: mensaje,
      },
      data: dataPayload,
      tokens: tokens,
    };

    try {
      // Enviar notificación a múltiples dispositivos
      const response = await getMessaging().sendEachForMulticast(message);

      logger.info(
        `Enviadas ${response.successCount} notificaciones ` +
        `exitosas de ${tokens.length} intentos`
      );

      // Log de errores si los hay
      if (response.failureCount > 0) {
        const failedTokens: string[] = [];
        response.responses.forEach((resp, idx) => {
          if (!resp.success) {
            failedTokens.push(tokens[idx]);
            logger.error(
              `Error al enviar a token ${tokens[idx]}: ${resp.error}`
            );
          }
        });
        logger.warn(`Tokens que fallaron: ${failedTokens.join(", ")}`);
      }

      // Eliminar el documento después de procesarlo
      await snap.ref.delete();
      logger.info("Documento de notificación procesado y eliminado");
    } catch (error) {
      logger.error("Error al enviar notificaciones:", error);
      // No eliminar el documento si hubo error, para reintentarlo
    }
  }
);
