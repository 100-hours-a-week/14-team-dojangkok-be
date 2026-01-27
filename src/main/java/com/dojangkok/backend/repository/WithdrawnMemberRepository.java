package com.dojangkok.backend.repository;

import com.dojangkok.backend.domain.WithdrawnMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WithdrawnMemberRepository extends JpaRepository<WithdrawnMember, Long> {
}
