package pisi.unitedmeows.seriex.util.yystal;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import pisi.unitedmeows.yystal.YSettings;
import pisi.unitedmeows.yystal.YYStal;
import pisi.unitedmeows.yystal.parallel.*;
import pisi.unitedmeows.yystal.utils.kThread;

public class FixedTaskPool implements ITaskPool {
	/* taskworkers that runs the tasks */
	private List<TaskWorker> taskWorkers;
	/* controller thread that controls worker */
	private Thread controlThread;
	/* maximum and the minimum number of workers in a pool */
	private int minWorkers , maxWorkers;
	/* tasks in the queue */
	private ConcurrentLinkedQueue<Task> taskQueue;
	private Map<Task, Long> waitingTasks;

	public FixedTaskPool(int minWorkers, int maxWorkers) {
		this.minWorkers = minWorkers;
		this.maxWorkers = maxWorkers;
		taskWorkers = new CopyOnWriteArrayList<>();
		waitingTasks = new ConcurrentHashMap<>();
		taskQueue = new ConcurrentLinkedQueue<>();
	}

	/* gets called when the client starts using this pool */
	@Override
	public void register() {
		controlThread = new Thread(this::control);
		for (int i = 0; i < minWorkers; i++) {
			final TaskWorker taskWorker = new TaskWorker();
			taskWorkers.add(taskWorker);
			taskWorker.start();
		}
		controlThread.start();
	}

	/* gets called when client switches the pool for another pool */
	@Override
	public void unregister() {
		for (TaskWorker taskWorker : taskWorkers) {
			taskWorker.stopWorker();
		}
		taskWorkers.clear();
		/* maybe add unfinished tasks to new pool? */
		taskQueue.clear();
	}

	/* controller thread */
	public void control() {
		while (YYStal.mainThread().isAlive()) {
			boolean allBusy = true;
			for (TaskWorker taskWorker : taskWorkers) {
				if (!taskWorker.isBusy()) {
					allBusy = false;
					break;
				}
			}
			/* if all workers are busy, adds new workers on the way */
			if (allBusy && workerCount() < maxWorkers) {
				final TaskWorker taskWorker = new TaskWorker();
				taskWorkers.add(taskWorker);
				taskWorker.start();
			}
			if (!waitingTasks.isEmpty()) {
				try {
					Iterator<Map.Entry<Task, Long>> waitingTasksIterator = waitingTasks.entrySet().iterator();
					long time = System.currentTimeMillis();
					while (waitingTasksIterator.hasNext()) {
						Map.Entry<Task, Long> taskTuple = waitingTasksIterator.next();
						if (taskTuple.getValue() < time) {
							taskQueue.add(taskTuple.getKey());
							waitingTasksIterator.remove();
						}
					}
				}
				catch (ConcurrentModificationException e) {
					e.printStackTrace();
				}
			}
			/* if a worker is free for some time removes the worker */
			if (!allBusy && workerCount() > minWorkers) {
				long time = System.currentTimeMillis() - 500 /* if worker is free more than 500ms */;
				Iterator<TaskWorker> taskWorkerIterator = taskWorkers.iterator();
				while (taskWorkerIterator.hasNext()) {
					if (workerCount() > minWorkers) {
						final TaskWorker taskWorker = taskWorkerIterator.next();
						if (taskWorker.lastTaskFinish() < time) {
							taskWorkerIterator.remove();
						}
					}
				}
			}
			/* waits  */
			kThread.sleep(YYStal.setting(YSettings.TASKPOOL_CONTROL_CHECK_DELAY));
		}
	}

	/* runs the function async */
	@Override
	public Task run(IFunction<?> function, Future<?> future) {
		final Task task = new Task(function, future);
		taskQueue.add(task);
		return task;
	}

	@Override
	public Task run_w(IFunction<?> function, Future<?> future, long after) {
		if (after <= 0) return run(function, future);
		final Task task = new Task(function, future);
		waitingTasks.put(task, System.currentTimeMillis() + after);
		return task;
	}

	@Override
	public TaskWorker getWorker() {
		Thread thread = Thread.currentThread();
		Iterator<TaskWorker> taskWorkerIterator = taskWorkers.iterator();
		while (taskWorkerIterator.hasNext()) {
			final TaskWorker taskWorker = taskWorkerIterator.next();
			if (taskWorker == thread) return taskWorker;
		}
		return null;
	}

	@Override
	public TaskWorker getWorker(Task task) {
		Iterator<TaskWorker> taskWorkerIterator = taskWorkers.iterator();
		while (taskWorkerIterator.hasNext()) {
			final TaskWorker taskWorker = taskWorkerIterator.next();
			if (taskWorker.currentTask() == task) return taskWorker;
		}
		return null;
	}

	@Override
	public void stopWorker(TaskWorker worker, boolean abort) {
		Iterator<TaskWorker> taskWorkerIterator = taskWorkers.iterator();
		while (taskWorkerIterator.hasNext()) {
			final TaskWorker taskWorker = taskWorkerIterator.next();
			if (taskWorker == worker) {
				taskWorkerIterator.remove();
				break;
			}
		}
		if (abort) {
			worker.abortWorker();
		} else {
			worker.stopWorker();
		}
	}

	@Override
	public void stopWorker(TaskWorker worker) {
		stopWorker(worker, false);
	}

	@Override
	public void newWorker() {
		final TaskWorker taskWorker = new TaskWorker();
		taskWorkers.add(taskWorker);
		taskWorker.start();
	}

	@Override
	public int workerCount() {
		return taskWorkers.size();
	}

	@Override
	public Task nextTask() {
		return taskQueue.poll();
	}
}
