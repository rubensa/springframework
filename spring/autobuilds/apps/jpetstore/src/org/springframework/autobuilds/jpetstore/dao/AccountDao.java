package org.springframework.autobuilds.jpetstore.dao;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.autobuilds.jpetstore.domain.Account;

public interface AccountDao {

  Account getAccount(String username) throws DataAccessException;

  Account getAccount(String username, String password) throws DataAccessException;

  void insertAccount(Account account) throws DataAccessException;

  void updateAccount(Account account) throws DataAccessException;

	List getUsernameList() throws DataAccessException;

}
