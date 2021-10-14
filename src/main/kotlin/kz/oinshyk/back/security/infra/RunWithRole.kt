package kz.oinshyk.back.security.infra

import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder

private const val key = "run-with-role-wrapper"

fun <T> runWithRole(role: String = "SYSTEM", block: () -> T): T {
    val authentication = SecurityContextHolder.getContext().authentication
    val token = AnonymousAuthenticationToken(key, key, setOf(SimpleGrantedAuthority("ROLE_$role")))
    SecurityContextHolder.getContext().authentication = token
    val result = block()
    SecurityContextHolder.getContext().authentication = authentication
    return result
}
