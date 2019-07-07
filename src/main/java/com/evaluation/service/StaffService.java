package com.evaluation.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.evaluation.domain.Staff;
import com.evaluation.vo.PageVO;

import org.springframework.data.domain.Page;

public interface StaffService {
	public void register(Staff staff);

	public Optional<Staff> read(Long sno);

	public void modify(Staff staff);

	public void remove(Long sno);

	public Page<Staff> getList(long cno, PageVO vo);

	public Optional<List<Staff>> get360EvaluatedList(long cno, long tno);

	public Optional<List<Staff>> get360EvaluatorList(long cno, long tno, long sno);

	public Optional<List<Staff>> getMboEvaluatedList(long cno, long tno);

	public Optional<List<Staff>> getMboEvaluatorList(long cno, long tno, long sno);

	public void deleteByCno(long cno);

	public void deleteDistinctInfoByTnoCno(long tno, long cno);

	public Optional<Staff> findByEmail(String email);

	public Optional<Staff> findByCnoAndName(long cno, String name);

	public Optional<List<Staff>> findByCno(long cno);

	public Map<String, Object> getDistinctInfo(long cno, long tno);
}
