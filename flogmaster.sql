
CREATE USER 'spacex'@'%' IDENTIFIED BY 'spacex';

GRANT ALL ON *.* TO 'spacex'@'%';

-- GRANT ALL PRIVILEGES ON flogmaster.* To 'spacex'@'%' IDENTIFIED BY 'spacex';

CREATE DATABASE flogmaster;
USE flogmaster;
--
-- Database: `flogmaster`
--

-- --------------------------------------------------------

--
-- Table structure for table `playboard`
--

CREATE TABLE `playboard` (
  `playboardid` int(11) NOT NULL,
  `gameid` int(11) NOT NULL,
  `player` varchar(30) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'WAITING',
  `round` int(11) DEFAULT '1',
  `word` varchar(30) DEFAULT NULL,
  `score` int(11) DEFAULT '0',
  `oldscore` int(11) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `playboard`
--
ALTER TABLE `playboard`
  ADD PRIMARY KEY (`playboardid`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `playboard`
--
ALTER TABLE `playboard`
  MODIFY `playboardid` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=216;
