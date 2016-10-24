package uk.co.bluegecko.pay.tools.upload.file;


import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import com.lexicalscope.jewel.cli.ArgumentValidationException;
import com.lexicalscope.jewel.cli.Cli;
import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.HelpRequestedException;

import uk.co.bluegecko.pay.tools.upload.file.cli.UploadCmdLine;
import uk.co.bluegecko.pay.tools.upload.file.service.FileUploadService;


@SpringBootApplication
public class FileUploadApplication
{

	private static final Logger logger = LoggerFactory.getLogger( FileUploadApplication.class );

	public static void main( final String... args )
	{
		SpringApplication.run( FileUploadApplication.class, args );
	}

	@Bean
	public RestTemplate restTemplate( final RestTemplateBuilder builder )
	{
		return builder.build();
	}

	@Bean
	public CommandLineRunner processCommandLine( final FileUploadService fileUploadService )
	{
		return ( args ) ->
			{
				final Cli< UploadCmdLine > cli = CliFactory.createCli( UploadCmdLine.class );
				try
				{
					final UploadCmdLine commandLine = cli.parseArguments( args );

					fileUploadService.checkConnection( commandLine.host() );

					for ( final String arg : commandLine.arguments() )
					{
						processFile( fileUploadService, commandLine, arg );
					}
				}
				catch ( final HelpRequestedException ex )
				{
					logger.error( ex.getLocalizedMessage() );
				}
				catch ( final ArgumentValidationException | IllegalArgumentException ex )
				{
					logger.error( ex.getLocalizedMessage() );
				}
				catch ( final IOException ex )
				{
					logger.error( "Unable to connect to host" );
				}
			};
	}

	private void processFile( final FileUploadService fileUploadService, final UploadCmdLine commandLine,
			final String arg )
	{
		final File file = new File( commandLine.directory(), arg );

		if ( fileUploadService.isFileValid( file ) )
		{
			fileUploadService.uploadFile( commandLine.host(), file );
		}
	}

}