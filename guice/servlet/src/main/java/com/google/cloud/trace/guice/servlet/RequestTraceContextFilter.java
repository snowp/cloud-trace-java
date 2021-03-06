// Copyright 2016 Google Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.cloud.trace.guice.servlet;

import com.google.cloud.trace.SpanContextHandler;
import com.google.cloud.trace.core.SpanContext;
import com.google.cloud.trace.core.SpanContextFactory;
import com.google.cloud.trace.core.SpanContextHandle;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

@Singleton
public class RequestTraceContextFilter implements Filter {
  private final SpanContextFactory spanContextFactory;
  private final SpanContextHandler spanContextHandler;

  @Inject
  RequestTraceContextFilter(SpanContextFactory spanContextFactory, SpanContextHandler spanContextHandler) {
    this.spanContextFactory = spanContextFactory;
    this.spanContextHandler = spanContextHandler;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}

  @Override
  public void destroy() {}

  @Override
  public void doFilter(
      ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;

    String contextHeader = httpRequest.getHeader(SpanContextFactory.headerKey());
    SpanContext context;
    if (contextHeader != null) {
      context = spanContextFactory.fromHeader(contextHeader);
    } else {
      context = spanContextFactory.initialContext();
    }
    SpanContextHandle handle = spanContextHandler.attach(context);
    try {
      chain.doFilter(request, response);
    } finally {
      handle.detach();
    }
  }
}
