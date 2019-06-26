package com.evaluation.service;

import java.util.List;
import java.util.Optional;

import com.evaluation.domain.Department;
import com.evaluation.vo.PageVO;

import org.springframework.data.domain.Page;

public interface DepartmentService {

	public void register(Department department);

	public Optional<Department> read(long dno);

	public void modify(Department department);

	public void remove(long tno);

	public Page<Department> getListWithPaging(long cno, PageVO vo);

	public Optional<List<Department>> findByCnoSno(long cno, long sno);

	public Optional<Department> findByDepartment(long cno, String department1, String department2);
}
