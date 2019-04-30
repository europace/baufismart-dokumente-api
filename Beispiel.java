package de.hypoport.ep2.vorgang.dokumente.backend.services;

import de.hypoport.ep2.dokumentenverwaltung.modelextern.DokumentId;
import de.hypoport.ep2.dokumentenverwaltung.modelextern.OeffentlicherDokumentenSchluessel;
import de.hypoport.ep2.support.backend.http.header.HttpHeaderAsMultiValueMapProvider;
import de.hypoport.ep2.support.backend.servicedocument.ModuleContext;
import de.hypoport.ep2.support.backend.servicedocument.ServiceProxyUrl;
import de.hypoport.ep2.support.types.exception.RemoteServiceExceptionWithErrorMessageForUser;
import de.hypoport.ep2.support.types.http.HttpStatusCode;
import de.hypoport.ep2.support.types.mime.MimeType;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import static de.hypoport.ep2.dokumentenverwaltung.rest.DokumenteRestKonstanten.BEZEICHNER_PARAM;
import static de.hypoport.ep2.dokumentenverwaltung.rest.DokumenteRestKonstanten.COLLECTION_RESOURCE_PFAD;
import static de.hypoport.ep2.dokumentenverwaltung.rest.DokumenteRestKonstanten.FILE_PARAM;
import static de.hypoport.ep2.dokumentenverwaltung.rest.DokumenteRestKonstanten.VORGANG_ID_PARAM;
import static javax.ws.rs.client.Entity.entity;

@Singleton
@Named
class DokumentenVerwaltungUploadClient implements DokumentUploadClient {

  Logger log = Logger.getLogger(DokumentenVerwaltungUploadClient.class);

  @Inject
  HttpHeaderAsMultiValueMapProvider headerProvider;

  @Inject
  ServiceProxyUrl serviceProxyUrl;

  @Override
  public DokumentUploadResponse upload(InputStream inputStream, MimeType mimeType, String filename, String bezeichner, String vorgangId) throws IOException {
    Response response;
    try (FormDataMultiPart form = new FormDataMultiPart()) {
      form.bodyPart(new StreamDataBodyPart(FILE_PARAM,
                                           inputStream,
                                           filename,
                                           getMediaType(mimeType)));

      if (StringUtils.isNotBlank(bezeichner)) {
        form.field(BEZEICHNER_PARAM, bezeichner);
      }
      if (StringUtils.isNotBlank(vorgangId)) {
        form.field(VORGANG_ID_PARAM, vorgangId);
      }

      final Invocation.Builder builder = createFileUploadTarget()
          .request()
          .headers(headerProvider.getHeadersWithAkteurToken())
          .header("Accept", "application/json");
      response = builder.post(entity(form, form.getMediaType()));
    }
    assertResponseCreated(response, mimeType);
    return parse(response);
  }

  MediaType getMediaType(MimeType mimeType) {
    return mimeType == null ? null : new MediaType(mimeType.getType(), mimeType.getSubtype());
  }

  private WebTarget createFileUploadTarget() {
    final String fileImportUri = serviceProxyUrl.module(ModuleContext.DOKUMENTENVERWALTUNG).path(COLLECTION_RESOURCE_PFAD).build();
    return ClientBuilder.newBuilder()
        .withConfig(new ClientConfig().connectorProvider(new HttpUrlConnectorProvider().useFixedLengthStreaming()))
        .register(MultiPartFeature.class)
        .build().target(fileImportUri);
  }

  private DokumentUploadResponse parse(Response source) {
    final DokumentUploadResponse target = new DokumentUploadResponse();
    JSONObject jsonSource = new JSONObject(source.readEntity(String.class));
    target.dokumentId = new DokumentId(jsonSource.getString("id"));
    target.schluessel = new OeffentlicherDokumentenSchluessel(jsonSource.getString("schluessel"));
    target.uri = URI.create(jsonSource.getString("url"));
    target.bezeichner = jsonSource.getString("bezeichner");
    target.mimeType = jsonSource.getString("mimeType");
    target.size = jsonSource.getLong("size");
    return target;
  }

  private void assertResponseCreated(Response response, final MimeType mimeType) {
    if (response.getStatus() != HttpStatusCode.CREATED) {
      String responseBody = response.readEntity(String.class);
      JSONObject errorDetails = new JSONObject(responseBody);
      if (mimeTypeWirdNichtUnterstuetzt(mimeType) || response.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY.value()) {
        log.info("Fehler beim hochladen des Dokuments in die Dokumentenverwaltung. Server antwortete mit Status " + response.getStatus() + ", content:" + responseBody);
      }
      else {
        log.error("Fehler beim hochladen des Dokuments in die Dokumentenverwaltung. Server antwortete mit Status " + response.getStatus() + ", content:" + responseBody);
      }
      throw new RemoteServiceExceptionWithErrorMessageForUser(errorDetails.getString("errorMessageForUser"));
    }
  }

  private boolean mimeTypeWirdNichtUnterstuetzt(final MimeType mimeType) {
    return mimeType == MimeType.TEXT_HTML;
  }
}
