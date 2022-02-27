// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.users;

import dk.ule.oapenwb.base.ErrorCode;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.entity.basis.Ban;
import dk.ule.oapenwb.entity.basis.Violation;
import dk.ule.oapenwb.entity.basis.ViolationType;
import dk.ule.oapenwb.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * <p>The ViolationController is part of a protection set of the backend and as such it handles the following
 * functionalities:
 * <ul>
 *   <li>TODO</li>
 * </ul>
 * </p>
 */
public class ViolationController
{
	private static final Logger LOG = LoggerFactory.getLogger(ViolationController.class);

	public void checkForBan(String ipAddr) throws CodeException
	{
		Session session = HibernateUtil.getSession();
		Query<Ban> query = session.createQuery(
			"FROM Ban b WHERE b.ip = :ip AND b.bannedUntil >= :now",
			Ban.class);
		query.setParameter("ip", ipAddr);
		query.setParameter("now", Instant.now());
		List<Ban> result = query.getResultList();

		if (result.size() > 0) {
			LOG.warn("Banned remote host tried to do another request");
			throw new CodeException(ErrorCode.General_IpBanned);
		}
	}

	public void createAndHandleViolations(ViolationType type, String ipAddr, String info)
	{
		Session session = HibernateUtil.getSession();
		Transaction t = session.beginTransaction();
		// Create a violation
		session.save(new Violation(Instant.now(), ipAddr, type, info));
		t.commit();

		// TODO Mask the IP address partially? (DSGVO)
		LOG.info("Violation (type: " + type.toString() + ") created for IP addr " + ipAddr);

		// Check if the violation count reaches the limit for a ban
		int count = getViolationCount(type, ipAddr);
		if (count >= type.getCountTilBan()) {
			// Create a ban
			t = session.beginTransaction();
			Instant bannedUntil = Instant.now().plus(type.getBanTime(), ChronoUnit.MINUTES);
			session.save(new Ban(ipAddr, bannedUntil));
			t.commit();

			// TODO Mask the IP address partially? (DSGVO)
			LOG.warn("Ban created for IP addr " + ipAddr);
		}
	}

	public int getViolationCount(ViolationType type, String ipAddr)
	{
		Instant boderTime = Instant.now().minus(type.getTimeFrame(), ChronoUnit.MINUTES);

		Session session = HibernateUtil.getSession();
		Query<Violation> qViolation = session.createQuery(
			"FROM Violation v WHERE v.key.ip = :ip AND v.key.whenTS >= :whenTS AND v.type = :type",
			Violation.class);
		qViolation.setParameter("type", type);
		qViolation.setParameter("ip", ipAddr);
		qViolation.setParameter("whenTS", boderTime);
		List<Violation> result = qViolation.getResultList();

		return result.size();
	}
}