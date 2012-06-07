Scenario first run
Then beforeClass should be 1
Then before should be 1
Then after should be 0

Scenario second run
Then beforeClass should be 1
Then before should be 2
#And before should not change.
Then before should be 2
Then after should be 1