package me.geso.mech2;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * HTTP request result.
 *
 * @author tokuhirom
 *
 */
public class Mech2Result {
	private final HttpRequest request;
	private final HttpResponse response;
	private final Mech2 mech2;

	Mech2Result(HttpRequest request, HttpResponse response, Mech2 mech2) {
		this.request = request;
		this.response = response;
		this.mech2 = mech2;
		try {
			byte[] body = EntityUtils.toByteArray(response.getEntity());
			ByteArrayEntity entity = new ByteArrayEntity(body);
			response.setEntity(entity);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public HttpRequest getRequest() {
		return this.request;
	}

	public HttpResponse getResponse() {
		return this.response;
	}

	public boolean isSuccess() {
		int statusCode = this.response.getStatusLine().getStatusCode();
		return 200 <= statusCode && statusCode < 300;
	}

	public <T> Mech2JSONResult<T> toJSONResult(Class<T> valueType) {
		return Mech2JSONResult.of(this, valueType);
	}

	public <T> Mech2JSONResult<T> toJSONResult(TypeReference<T> klass) {
		return Mech2JSONResult.of(this, klass);
	}

	public <T> T parseJSON(TypeReference<T> valueType)
			throws JsonParseException, JsonMappingException, IOException {
		return this.mech2.getObjectMapper().readValue(this.getResponse().getEntity().getContent(), valueType);
	}

	public <T> T parseJSON(Class<T> valueType) throws JsonParseException,
			JsonMappingException, IOException {
		return this.mech2.getObjectMapper().readValue(this.getResponse().getEntity().getContent(), valueType);
	}

	/**
	 * throw exception if the response doesn't have 2xx response.
	 *
	 * @throws Mech2FailException
	 */
	public Mech2Result orDie() throws Mech2FailException {
		if (this.isSuccess()) {
			return this;
		} else {
			throw new Mech2FailException(this);
		}
	}

	public String getResponseBodyAsString() throws ParseException, IOException {
		ContentType contentType = ContentType.getOrDefault(this.response.getEntity());
		Charset charset = contentType.getCharset() == null ? StandardCharsets.ISO_8859_1 : contentType.getCharset();
		return EntityUtils.toString(this.response.getEntity(), charset);
	}
}
