package com.st.trex.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.st.trex.serviceimpl.UserDetailsServiceImpl;

import io.jsonwebtoken.ExpiredJwtException;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private UserDetailsServiceImpl userDetailsServiceImpl;
	
	@Autowired
	private JWTUtil jwtUtil;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		final String requestTokenHeader=request.getHeader("Authorization");
		System.out.println("RequestTokenHeader "+requestTokenHeader);
		String username=null;
		String jwtToken=null;
		if(requestTokenHeader!=null && requestTokenHeader.startsWith("Bearer ")) {
			
			jwtToken=requestTokenHeader.substring(7);
			try {
			username=this.jwtUtil.extractUsername(jwtToken);
			System.out.println("Username = "+username);
			}
			catch(ExpiredJwtException e) {
				System.err.println("JWT Token has expired");
				e.printStackTrace();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}else {
			System.out.println("Invalid Token, not start with Bearer");
		}
		
		/**
		 * Now use validation
		 */
		
		if(username!=null && SecurityContextHolder.getContext().getAuthentication()==null) {
			final UserDetails userDetails=this.userDetailsServiceImpl.loadUserByUsername(username);
			if(this.jwtUtil.validateToken(jwtToken, userDetails)) {
				// Token is valid
				System.out.println("Call inside JWT Authentication filter valodation IF");
				UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken=new UsernamePasswordAuthenticationToken(userDetails, null,userDetails.getAuthorities());
				usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
			}
		}else {
			System.out.println("Token is not valid");
		}
		filterChain.doFilter(request, response);
	}

}
