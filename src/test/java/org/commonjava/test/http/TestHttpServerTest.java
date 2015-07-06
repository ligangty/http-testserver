package org.commonjava.test.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Rule;
import org.junit.Test;

public class TestHttpServerTest
{

    @Rule
    public TestHttpServer server = new TestHttpServer( "repos" );

    @Test
    public void simpleDownload()
        throws Exception
    {
        final String path = "/repos/path/to/something.txt";
        final String content = "this is the content";
        final String url = server.formatUrl( path );
        server.expect( url, 200, content );

        final HttpGet request = new HttpGet( url );
        final CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = null;

        InputStream stream = null;
        try
        {
            response = client.execute( request );
            stream = response.getEntity()
                             .getContent();
            final String result = IOUtils.toString( stream );

            assertThat( result, notNullValue() );
            assertThat( result, equalTo( content ) );
        }
        finally
        {
            IOUtils.closeQuietly( stream );
            if ( response != null && response.getEntity() != null )
            {
                EntityUtils.consumeQuietly( response.getEntity() );
                IOUtils.closeQuietly( response );
            }

            if ( request != null )
            {
                request.reset();
            }

            if ( client != null )
            {
                IOUtils.closeQuietly( client );
            }
        }

        assertThat( server.getAccessesByPathKey()
                          .get( server.getAccessKey( CommonMethod.GET.name(), path ) ), equalTo( 1 ) );
    }

}