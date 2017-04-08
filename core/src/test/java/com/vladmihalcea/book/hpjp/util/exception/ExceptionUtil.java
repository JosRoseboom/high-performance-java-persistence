package com.vladmihalcea.book.hpjp.util.exception;

import java.sql.SQLTimeoutException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.persistence.LockTimeoutException;

import org.hibernate.PessimisticLockException;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.exception.LockAcquisitionException;

/**
 * @author Vlad Mihalcea
 */
public interface ExceptionUtil {

	static List<Class<? extends Exception>> LOCK_TIMEOUT_EXCEPTIONS = Arrays.asList(
		LockAcquisitionException.class,
		LockTimeoutException.class,
		PessimisticLockException.class,
		javax.persistence.PessimisticLockException.class,
		SQLTimeoutException.class
	);

	/**
	 * Get the root cause of a particular {@code Throwable}
	 *
	 * @param t exception
	 *
	 * @return exception root cause
	 */
	static Throwable rootCause(Throwable t) {
		Throwable cause = t.getCause();
		if ( cause != null && cause != t ) {
			return rootCause( cause );
		}
		return t;
	}

	/**
	 * Is the given throwable caused by a database lock timeout?
	 *
	 * @param e exception
	 *
	 * @return is caused by a database lock timeout
	 */
	static boolean isLockTimeout(Throwable e) {
		AtomicReference<Throwable> causeHolder = new AtomicReference<>(e);
		do {
			final Throwable cause = causeHolder.get();
			if ( LOCK_TIMEOUT_EXCEPTIONS.stream().anyMatch( c -> c.isInstance( cause ) ) ||
				e.getMessage().contains( "timeout" ) ||
				e.getMessage().contains( "timed out" ) ||
				e.getMessage().contains( "time out" )
			) {
				return true;
			} else {
				if(cause.getCause() == null || cause.getCause() == cause) {
					break;
				} else {
					causeHolder.set( cause.getCause() );
				}
			}
		}
		while ( true );
		return false;
	}

	/**
	 * Was the given exception caused by a SQL connection close
	 *
	 * @param e exception
	 *
	 * @return is caused by a SQL connection close
	 */
	static boolean isConnectionClose(Exception e) {
		Throwable cause = e;
		do {
			if ( cause.getMessage().toLowerCase().contains( "connection is close" ) ||
				cause.getMessage().toLowerCase().contains( "closed connection" )
			) {
				return true;
			} else {
				if(cause.getCause() == null || cause.getCause() == cause) {
					break;
				} else {
					cause = cause.getCause();
				}
			}
		}
		while ( true );
		return false;
	}
}