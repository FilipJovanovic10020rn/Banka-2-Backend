package rs.edu.raf.si.bank2.client.filters;

import io.jsonwebtoken.MalformedJwtException;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import rs.edu.raf.si.bank2.client.services.interfaces.UserServiceInterface;
import rs.edu.raf.si.bank2.client.utils.JwtUtil;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final UserServiceInterface userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public JwtFilter(UserServiceInterface userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = httpServletRequest.getHeader("Authorization");
        String jwt = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (MalformedJwtException e) {
                username = null;
            }
        }

        if (username != null) {

            // todo vrati ovaj kurac
            //            UserDetails userDetails = this.userService.loadUserByUsername(username);
            //            if (jwtUtil.validateToken(jwt, userDetails)) {
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                    new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
            usernamePasswordAuthenticationToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            //            }

        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
