/*-
 * #%L
 * TODO
 * %%
 * Copyright (C) 2018 - 2020 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.tables.github;

public class GitHubFileCommitter
{
	private String repository;
	private String accessToken;
	private String path;

	public GitHubFileCommitter( String repository, String accessToken, String path )
	{
		this.repository = repository;
		this.accessToken = accessToken;
		this.path = path;
	}

	public void commitStringAsFile( String message, String base64String )
	{
		final GitHubFileCommit fileCommit = new GitHubFileCommit( message, base64String );
		String url = createFileCommitApiUrl( path );
		final String requestMethod = "PUT";
		final String json = fileCommit.toString();

		new RESTCaller().put( url, requestMethod, json, accessToken );
	}

	public String createFileCommitApiUrl( String path )
	{
		String url = repository.replace( "github.com", "api.github.com/repos" );
		if ( ! url.endsWith( "/" ) ) url += "/";
		if ( ! path.startsWith( "/" ) ) path = "/" + path;
		url += "contents" + path;
		return url;
	}
}
