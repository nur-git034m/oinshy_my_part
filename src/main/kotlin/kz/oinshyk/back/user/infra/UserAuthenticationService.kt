package kz.oinshyk.back.user.infra

import kz.oinshyk.back.security.infra.runWithRole
import kz.oinshyk.back.user.domain.port.AppUserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Suppress("unused")
@Service
class UserAuthenticationService(val appUserRepository: AppUserRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String?): UserDetails {
        if (username != null) {
            val appUser = runWithRole {
                appUserRepository.findByLogin(username)
                        ?: throw UsernameNotFoundException("User $username not found.")
            }
            return User(username, appUser.password, setOf(SimpleGrantedAuthority("ROLE_${appUser.role}")))
        }
        throw UsernameNotFoundException("Username is not provided.")
    }
}
