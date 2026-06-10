package br.com.ton.estudos.data.repository

import br.com.ton.estudos.data.local.database.dao.SubjectDao
import br.com.ton.estudos.data.local.database.entity.SubjectEntity
import br.com.ton.estudos.domain.model.Subject
import br.com.ton.estudos.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubjectRepositoryImpl @Inject constructor(
    private val subjectDao: SubjectDao
) : SubjectRepository {
    override fun getAllSubjects(): Flow<List<Subject>> =
        subjectDao.getAllSubjects().map { list -> list.map { it.toDomain() } }

    override suspend fun getSubjectById(id: Long): Subject? =
        subjectDao.getSubjectById(id)?.toDomain()

    override suspend fun insertSubject(subject: Subject): Long =
        subjectDao.insertSubject(subject.toEntity())

    override suspend fun updateSubject(subject: Subject) =
        subjectDao.updateSubject(subject.toEntity())

    override suspend fun deleteSubject(subject: Subject) =
        subjectDao.deleteSubject(subject.toEntity())

    override fun getSubjectCount(): Flow<Int> = subjectDao.getSubjectCount()

    private fun SubjectEntity.toDomain() = Subject(id, name, color, icon, createdAt)
    private fun Subject.toEntity() = SubjectEntity(id, name, color, icon, createdAt)
}
