package com.evaluation.service.Impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.evaluation.domain.Book;
import com.evaluation.persistence.BookRepository;
import com.evaluation.service.BookService;

@Service
public class BookServiceImpl implements BookService {

	@Autowired
	BookRepository bookRepo;

	@Override
	public void register(Book book) {
		bookRepo.save(book);
	}

	@Override
	public Optional<Book> read(Long bno) {
		return bookRepo.findById(bno);
	}

	@Override
	public void modify(Book book) {
		bookRepo.save(book);
	}

	@Override
	public void remove(Long bno) {
		bookRepo.deleteById(bno);
	}

	@Override
	public List<Book> list() {
		Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC, "bno"));
		List<Book> result = bookRepo.findAll(sort);
		return result;
	}

}