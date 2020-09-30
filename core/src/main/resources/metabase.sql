# Issue数量 By Author
SELECT `author`.`name` AS `author`, count(*) AS `count`
FROM `issue`
         inner join `author` on `author`.issue_id = `issue`.id
GROUP BY `issue`.`author`
ORDER BY `count` DESC;

# issue参与数
SELECT `participant`.`name` AS `name`, count(*) AS `count`
FROM `participant`
WHERE `participant`.`name` <> 'Friday'
GROUP BY `participant`.`name`
ORDER BY `count` DESC
limit 15;

# 人员当前工作量
SELECT `issue`.`assignee`->>'$.name' as `处理人`, count(*) AS `count`
FROM `issue`
where state='opened' and labels not like '%verify%'
GROUP BY `issue`.`assignee`
order by `count`
;

# 任务进度
SELECT case
           when `issue`.labels like '%To Do%' then "To Do"
           when labels like '%Doing%' then "Doing"
           when labels like '%verify%' then "Verify"
           ELSE "BACKLOG"
           END as 'progress', count(*) AS `count`
FROM `issue`
where state='opened'
and milestone->> '$.title'= date_format(now(), '%Y%m')
GROUP BY progress
;

# 按milestone统计bug数量
SELECT milestone->> '$.title' as '里程碑', count(*) AS `count`
FROM `issue`
WHERE (lower(`issue`.`labels`) like '%bug%') and milestone->> '$.title' is not null
  and milestone->> '$.title' <> 'sword'
GROUP BY 里程碑;

# issue版本规划
SELECT  count(*) AS `count`,milestone->> '$.title', date_format(`issue`.`created_at`, '%Y%m'),
        case
            when date_format(`issue`.`created_at`, '%Y%m') < milestone->> '$.title' then "计划"
            ELSE "新增"
            END as 'plan'
FROM `issue`
WHERE milestone->> '$.title'= date_format(now(), '%Y%m')
  and `issue`.`labels` not like '%bug%'
  and `issue`.`labels` like '%P%'
GROUP BY plan